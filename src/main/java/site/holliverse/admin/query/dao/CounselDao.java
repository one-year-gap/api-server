package site.holliverse.admin.query.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.DatePart;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.extract;
import static site.holliverse.admin.query.jooq.tables.SupportCase.SUPPORT_CASE;

@RequiredArgsConstructor
@Repository
@Profile("admin")
public class CounselDao {
    private final DSLContext dsl;

    /**
     *
     * @param date
     * @return
     */
    public List<CounselTrafficDailyRawData> fetchCounselTrafficByHour(LocalDate date) {
        Field<Integer> timeZone = extract(SUPPORT_CASE.CREATED_AT, DatePart.HOUR)
                .cast(Integer.class)
                .as("hour");
        Field<Integer> trafficCount = count().as("count");

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        return dsl
                .select(timeZone, trafficCount)
                .from(SUPPORT_CASE)
                .where(SUPPORT_CASE.CREATED_AT.ge(start).and(SUPPORT_CASE.CREATED_AT.lt(end)))
                .groupBy(timeZone)
                .orderBy(timeZone.asc())
                .fetch(record -> new CounselTrafficDailyRawData(
                        record.get(timeZone),
                        record.get(trafficCount)
                ));
    }

    public List<CounselTrafficMonthlyRawData> fetchCounselTrafficByDay(YearMonth month) {
        Field<Integer> day = extract(SUPPORT_CASE.CREATED_AT, DatePart.DAY)
                .cast(Integer.class)
                .as("day");
        Field<Integer> trafficCount = count().as("count");

        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();

        return dsl
                .select(day, trafficCount)
                .from(SUPPORT_CASE)
                .where(SUPPORT_CASE.CREATED_AT.ge(start).and(SUPPORT_CASE.CREATED_AT.lt(end)))
                .groupBy(day)
                .orderBy(day.asc())
                .fetch(record -> new CounselTrafficMonthlyRawData(
                        record.get(day),
                        record.get(trafficCount)
                ));
    }
}
