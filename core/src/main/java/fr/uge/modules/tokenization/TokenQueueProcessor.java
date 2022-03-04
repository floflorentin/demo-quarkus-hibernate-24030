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
                    log.setTokens(List.of(token, token, token));
                    return log;
                })
                .group()
                .intoLists()
                .every(Duration.ofMillis(10));
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
