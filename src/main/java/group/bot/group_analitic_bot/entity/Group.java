package group.bot.group_analitic_bot.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Entity(name = "groups")
public class Group {
    @Id
    private Long id;

    private String username;

    @Column(nullable = false)
    private Integer addCount;
}
