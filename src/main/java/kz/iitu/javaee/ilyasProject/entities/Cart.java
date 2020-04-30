package kz.iitu.javaee.ilyasProject.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_cart")
public class Cart extends BaseEntity{


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product")
    private Products product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user")
    private Users user;
}
