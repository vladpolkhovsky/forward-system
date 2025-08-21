package by.forward.forward_system.core.mapper;

import by.forward.forward_system.core.dto.messenger.v3.chat.info.V3OrderReviewDto;
import by.forward.forward_system.core.jpa.model.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
    AttachmentMapper.class
})
public interface ReviewMapper {

    @Mapping(target = "resultText", source = "reviewVerdict")
    @Mapping(target = "resultMark", source = "reviewMark")
    @Mapping(target = "resultFile", source = "reviewAttachment")
    @Mapping(target = "resultDate", source = "reviewDate", dateFormat = "dd.MM.yyyy HH:mm")
    @Mapping(target = "requestText", source = "reviewMessage")
    @Mapping(target = "requestFile", source = "attachment")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "isApproved", source = "isReviewed")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "dd.MM.yyyy HH:mm")
    V3OrderReviewDto map(ReviewEntity reviewEntity);

    List<V3OrderReviewDto> mapMany(List<ReviewEntity> reviewEntities);
}
