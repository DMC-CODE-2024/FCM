package com.eirs.fcm.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpFileDto {
    @JsonProperty("txnId")
    private String txnId;
    @JsonProperty("serverName")
    private String serverName;

    @JsonProperty("sourceServerName")
    private String sourceServerName;
    @JsonProperty("sourceFilePath")
    private String sourceFilePath;
    @JsonProperty("sourceFileName")
    private String sourceFileName;
    @JsonProperty("destination")
    private List<SftpDestinationDto> destination;
    @JsonProperty("appName")
    private String applicationName;
    @JsonProperty("remarks")
    private String remarks;
    @JsonProperty("fileType")
    private String fileType;
}
