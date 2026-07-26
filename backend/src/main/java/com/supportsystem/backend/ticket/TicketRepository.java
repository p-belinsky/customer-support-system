package com.supportsystem.backend.ticket;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

	Page<Ticket> findByCategory(String category, Pageable pageable);

	Page<Ticket> findByStatusAndCategory(TicketStatus status, String category, Pageable pageable);

	@Query("SELECT DISTINCT t.category FROM Ticket t WHERE t.category IS NOT NULL ORDER BY t.category")
	List<String> findDistinctCategories();

	@Query("SELECT t.status AS status, COUNT(t) AS count FROM Ticket t GROUP BY t.status")
	List<StatusCountRow> countByStatusGrouped();

	@Query("SELECT COALESCE(t.category, 'Uncategorized') AS category, COUNT(t) AS count "
			+ "FROM Ticket t GROUP BY COALESCE(t.category, 'Uncategorized')")
	List<CategoryCountRow> countByCategoryGrouped();

	interface StatusCountRow {
		TicketStatus getStatus();

		long getCount();
	}

	interface CategoryCountRow {
		String getCategory();

		long getCount();
	}
}
