package repository;

import model.SimulationResult;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SimulationResultRepository implements ICsvRepository<SimulationResult> {

    private final CsvRepository<SimulationResult> csv;

    public SimulationResultRepository() {
        this.csv = new CsvRepository<>("data/simulation_results.csv", SimulationResult::new);
    }

    public SimulationResultRepository(String filePath) {
        this.csv = new CsvRepository<>(filePath, SimulationResult::new);
    }

    @Override
    public List<SimulationResult> findAll() {
        return csv.findAll();
    }

    @Override
    public Optional<SimulationResult> findById(String id) {
        return csv.findById(id);
    }

    @Override
    public List<SimulationResult> findByCondition(Predicate<SimulationResult> condition) {
        return csv.findByCondition(condition);
    }

    @Override
    public boolean save(SimulationResult result) {
        return csv.save(result);
    }

    @Override
    public boolean deleteById(String id) {
        return csv.deleteById(id);
    }

    @Override
    public int count() {
        return csv.count();
    }
}
