package kz.iitu.javaee.ilyasProject.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_cotegories")
public class Categories extends BaseEntity implements GrantedAuthority {

    @Column(name = "category")
    private String category;

    @Override
    public String getAuthority() {
        return this.category;
    }
}