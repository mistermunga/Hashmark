package xyz.ryansbeanfactory.hashmark.hashmark.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter @Setter
public class HashedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String imageID;

    @Column
    private String imageName;
    
    @Column
    private String imagePath;

    @Lob
    @Column(nullable = false)
    private String rootHash;

    @ManyToOne
    @JoinColumn
    private User signedBy;

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

}
