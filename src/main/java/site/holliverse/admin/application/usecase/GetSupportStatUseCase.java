package site.holliverse.admin.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.query.dao.AdminSupportStatDao;
import site.holliverse.admin.query.dao.AdminSupportStatRawData;

@Profile("admin")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSupportStatUseCase {

    private final AdminSupportStatDao adminSupportStatDao;

    public AdminSupportStatRawData execute() {
        return adminSupportStatDao.getSupportStatusStats();
    }
}