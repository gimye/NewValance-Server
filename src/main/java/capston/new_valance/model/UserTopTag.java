package capston.new_valance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usertoptags")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTopTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userTagId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "tag_id")
    private int tagId;

    @Column(name = "weight")
    private float weight;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
