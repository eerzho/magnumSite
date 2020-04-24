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
@Table(name = "t_companies")
public class Companies extends BaseEntity implements GrantedAuthority {

    @Column(name = "company")
    private String company;

    @Override
    public String getAuthority() {
        return this.company;
    }
}