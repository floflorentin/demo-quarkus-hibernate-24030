package fr.uge.modules.tokenization;

import fr.uge.modules.api.model.entities.LogEntity;
import fr.uge.modules.api.model.entities.RawLogEntity;
import fr.uge.modules.api.model.entities.TokenEntity;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.annotations.Merge;
import io.vertx.core.json.JsonObject;
import org.jboss.logmanager.Level;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import javax.enterprise.context.ApplicationScoped;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class TokenQueueProcessor {
    private final Logger LOGGER = Logger.getGlobal();

    @Incoming(value = "token-in")
    @Outgoing(value = "batch-processor")
    public Multi<List<LogEntity>> processTokenization(Multi<JsonObject> incoming){
        return incoming.map(rawLog -> rawLog.mapTo(RawLogEntity.class))
                .map(rawLogEntity -> {
                    LogEntity log = new LogEntity();
                    log.setId(rawLogEntity.id);
                    log.setDatetime(Timestamp.from(Instant.now()));
                    TokenEntity token = new TokenEntity();
                    token.setValue(rawLogEntity.log);
                    token.setIdtokentype(1);
                    log.setTokens(List.of(token));
                    Arrays.stream(rawLogEntity.log.split("/t")).forEach(word -> {
                        // To emulate queue congestion and complete tokenization
                        if(word.matches("(([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){3}([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])")) {
                            token.setValue(rawLogEntity.log);
                        }
                        if(word.matches("(([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){3}([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])")) {
                            token.setValue(rawLogEntity.log);
                        }
                        if(word.matches("(([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){3}([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])")) {
                            token.setValue(rawLogEntity.log);
                        }
                        if(word.matches("(([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){3}([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])")) {
                            token.setValue(rawLogEntity.log);
                        }
                        if(word.matches("(([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){3}([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])")) {
                            token.setValue(rawLogEntity.log);
                        }
                        if(word.matches("(([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){3}([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])")) {
                            token.setValue(rawLogEntity.log);
                        }
                        if(word.matches("(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))")) {
                            token.setValue(rawLogEntity.log);
                        }
                    });
                    return log;
                })
                .group()
                .intoLists()
                .every(Duration.ofMillis(1000));
    }

    @Merge
    @Incoming(value = "batch-processor")
    public Uni<Void> processBatch(List<LogEntity> logs) {
        return Panache.withTransaction(() -> LogEntity.persist(logs))
                .onFailure()
                .invoke(error -> {
                    LOGGER.log(Level.SEVERE, "Error while inserting log id in database");
                })
                .replaceWithVoid();
    }
}
