package com.antonybresolin.springsecurity.controller.dto;

import java.util.List;

public record FeedDto(List<FeedItemDto> feedItems,
                      int page,
                      int pageSize,
                      int totalPages,
                      long totalElements) {
}
