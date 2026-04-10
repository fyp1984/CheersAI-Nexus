package com.cheersai.nexus.membership.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesktopMemberStatsResponse {

    private long totalMembers;
    private long dau;
    private long wau;
    private long mau;
    private long todayEvents;
    private long todayFeedbacks;
}
