package com.example.gestiondocuments.Repositories;

import com.example.gestiondocuments.Entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
