package com.ctrc.core.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PushService {

    private static final Logger log = LoggerFactory.getLogger(PushService.class);

    private static final String CHANNEL_ID = "ctrc_alerts";

    @Value("${firebase.credentials-path:/etc/secrets/firebase-service-account.json}")
    private String credentialsPath;

    private FirebaseApp app;

    @PostConstruct
    void init() {
        Path path = Path.of(credentialsPath);
        if (!Files.isReadable(path)) {
            log.warn("Push notifications are off: no Firebase credentials at {}", credentialsPath);
            return;
        }

        try (InputStream credentials = Files.newInputStream(path)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build();
            app = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(options)
                    : FirebaseApp.getInstance();
            log.info("Push notifications are on");
        } catch (Exception e) {
            log.warn("Push notifications are off: could not read {}", credentialsPath, e);
        }
    }

    public void notifyArea(Long reportId, String title, String category,
                           double latitude, double longitude) {
        if (app == null) {
            return;
        }

        String topic = GeoTopic.of(latitude, longitude);

        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(category + " reported nearby")
                            .setBody(title)
                            .build())

                    .putData("reportId", String.valueOf(reportId))
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setChannelId(CHANNEL_ID)
                                    .build())
                            .build())
                    .build();

            String id = FirebaseMessaging.getInstance(app).send(message);
            log.info("Pushed report {} to {} ({})", reportId, topic, id);
        } catch (Exception e) {
            log.warn("Could not push report {} to {}", reportId, topic, e);
        }
    }
}
