package group.bot.group_analitic_bot.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class UserGroup {

    public UserGroup(User user, Group group) {
        this.user = user;
        this.group = group;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Group group;

    private Boolean isActive;

    @PrePersist
    public void prePersist() {
        if (this.isActive == null) this.isActive = false;  // Agar qiymat o'rnatilmagan bo'lsa, false qilish
    }


}
