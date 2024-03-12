package com.planbow.repository;

import com.planbow.entities.user.UserEntity;
import com.planbow.util.data.support.repository.HibernateRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Repository
@Transactional
public class PlanbowHibernateRepository extends HibernateRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public UserEntity getUserEntity(Long id) {
        return (UserEntity) getEntity(UserEntity.class,id);
    }

    public List<UserEntity> getUserEntities(String email, List<String> userIds){
        CriteriaBuilder criteriaBuilder  = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> userEntityCriteriaQuery= criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> userEntityRoot= userEntityCriteriaQuery.from(UserEntity.class);
        userEntityCriteriaQuery.select(userEntityRoot).distinct(true);

        if (!TextUtils.isEmpty(email)) {
            userEntityCriteriaQuery.where(
                    criteriaBuilder.equal(userEntityRoot.get("email"),email));
        }
        if(!CollectionUtils.isEmpty(userIds)){
            userEntityCriteriaQuery.where(
                    userEntityRoot.get("id").in(userIds));
        }

        return entityManager.createQuery(userEntityCriteriaQuery).getResultList();
    }

}
