package se.simple.simplelog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "log", schema = "simplelog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(name = "Log.types", attributeNodes = {
        @NamedAttributeNode("type")
})
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device")
    private String device;

    @Column(name = "created", columnDefinition = "TIMESTAMP")
    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "type_ref")
    private LogType type;
}
