package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.ActivityControllerApi
import com.rarible.protocol.solana.common.continuation.ActivityContinuation
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.continuation.Paging
import com.rarible.protocol.solana.dto.*
import com.rarible.protocol.solana.nft.api.service.ActivityApiService
import com.rarible.protocol.union.dto.continuation.page.PageSize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivityController(
    private val activityApiService: ActivityApiService,
) : ActivityControllerApi {

    override suspend fun searchActivities(
        filter: ActivityFilterDto,
        continuation: String?,
        size: Int?,
        sort: ActivitySortDto?,
    ): ResponseEntity<ActivitiesDto> {

        val safeSort = sort == ActivitySortDto.EARLIEST_FIRST
        val safeSize = PageSize.ACTIVITY.limit(size)

        val dateIdContinuation = DateIdContinuation.parse(continuation)

        val result = when (filter) {
            is ActivityFilterAllDto -> activityApiService.getAllActivities(
                filter, dateIdContinuation, safeSize, safeSort
            )
            is ActivityFilterByItemDto -> activityApiService.getActivitiesByItem(
                filter, dateIdContinuation, safeSize, safeSort
            )
            is ActivityFilterByCollectionDto -> activityApiService.getActivitiesByCollection(
                filter, dateIdContinuation, safeSize, safeSort
            )
            is ActivityFilterByUserDto -> activityApiService.getActivitiesByUser(
                filter, dateIdContinuation, safeSize, safeSort
            )
        }

        val dto = toSlice(result, safeSort, safeSize)

        return ResponseEntity.ok(dto)
    }

    private fun toSlice(result: List<ActivityDto>, asc: Boolean, size: Int): ActivitiesDto {
        val continuationFactory =
            if (asc) ActivityContinuation.ByLastUpdatedAndIdAsc
            else ActivityContinuation.ByLastUpdatedAndIdDesc

        val slice = Paging(continuationFactory, result).getSlice(size)
        return ActivitiesDto(slice.continuation, slice.entities)
    }
}
