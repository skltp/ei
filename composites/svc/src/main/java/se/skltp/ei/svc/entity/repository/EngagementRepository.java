package se.skltp.ei.svc.entity.repository;

import org.springframework.data.repository.CrudRepository;

import se.skltp.ei.svc.entity.model.Engagement;

public interface EngagementRepository extends CrudRepository<Engagement, Long> {
}
