package by.forward.forward_system.core.utils;

import by.forward.forward_system.core.dto.ai.AiRequestDto;
import by.forward.forward_system.core.dto.ai.AiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @SneakyThrows
    public AiRequestDto mapAiRequest(String request) {
        return MAPPER.readValue(request, AiRequestDto.class);
    }

    @SneakyThrows
    public AiResponseDto mapAiResponse(String response) {
        return MAPPER.readValue(response, AiResponseDto.class);
    }
}
