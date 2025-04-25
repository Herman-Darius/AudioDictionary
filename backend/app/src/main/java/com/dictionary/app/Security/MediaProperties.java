package com.dictionary.app.Security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "media")
@Data
public class MediaProperties {

    private String audioDir = System.getProperty("user.dir") + "/uploads/audio_files/";
    private String imageDir = System.getProperty("user.dir") + "/uploads/image_files/";
}
