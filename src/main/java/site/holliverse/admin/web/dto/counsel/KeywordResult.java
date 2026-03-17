package site.holliverse.admin.web.dto.counsel;

public record KeywordResult(
        long keywordId,
        long businessKeywordId,
        String keywordCode,
        String keywordName,
        int count,
        int negativeWeight
) {
}
