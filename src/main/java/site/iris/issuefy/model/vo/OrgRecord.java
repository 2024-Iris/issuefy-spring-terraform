package site.iris.issuefy.model.vo;

import java.util.List;

import site.iris.issuefy.model.dto.RepositoryDto;

public record OrgRecord(Long id, String name, List<RepositoryDto> repositories) {
    public static OrgRecord from(Long id, String name, List<RepositoryDto> repositories) {
        return new OrgRecord(id, name, repositories);
    }

    @Override
    public String toString() {
        return "OrgRecord{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", repositories=" + repositories +
            '}';
    }
}