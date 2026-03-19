package site.holliverse.admin.query.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChurnRealtimeRawData {
    private Long churnId;
    private Long memberId;
    private String encryptedName;
    private String churnLevel;
    private String riskReasons;
    private OffsetDateTime timeStamp;
}
