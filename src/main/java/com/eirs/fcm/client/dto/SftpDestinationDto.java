package com.eirs.fcm.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpDestinationDto {
    @JsonProperty("destServerName")
    private String destServerName;

    @JsonProperty("destFilePath")
    private String destFilePath;

}
