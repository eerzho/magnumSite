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
@Table(name = "t_product")
public class Products extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Integer price;

    @Column(name = "description")
    private String description;

    @Column(name = "availability")
    private Boolean availability;

    @Column(name = "photo_name")
    private String photo_name;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Categories> category;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Companies> company;

}