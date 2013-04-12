package se.skltp.ei.svc.entity.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import se.skltp.ei.svc.entity.model.Engagement;

//
public interface EngagementRepository extends CrudRepository<Engagement, Engagement.Key> {
	List<Engagement> findByKeyIn(List<Engagement.Key> keys);
}
