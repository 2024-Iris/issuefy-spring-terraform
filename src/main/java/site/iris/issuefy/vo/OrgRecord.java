package site.iris.issuefy.vo;

import java.util.List;

public record OrgRecord(Long id, String name, List<RepositoryDto> repositories) {
    public static OrgRecord from(Long id, String name, List<RepositoryDto> repositories) {
        return new OrgRecord(id, name, repositories);
    }
}