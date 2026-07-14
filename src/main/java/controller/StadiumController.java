package controller;

import java.util.List;
import java.util.Optional;

import model.Section;
import model.Stadium;
import repository.SectionRepository;
import repository.StadiumRepository;

public class StadiumController {

    private final StadiumRepository stadiumRepo;
    private final SectionRepository sectionRepo;

    public StadiumController() {
        this.stadiumRepo = new StadiumRepository();
        this.sectionRepo = new SectionRepository();
    }

    public StadiumController(StadiumRepository stadiumRepo, SectionRepository sectionRepo) {
        this.stadiumRepo = stadiumRepo;
        this.sectionRepo = sectionRepo;
    }

    /**
     * Lấy danh sách tất cả các sân.
     * 
     * @return danh sách các sân
     */
    public List<Stadium> getAllStadiums() {
        return stadiumRepo.findAll();
    }

    /**
     * Tìm sân theo ID.
     * 
     * @param id ID của sân
     * @return Optional chứa sân nếu tìm thấy, ngược lại Optional rỗng
     */
    public Optional<Stadium> getStadiumById(String id) {
        return stadiumRepo.findById(id);
    }

    /**
     * Lấy danh sách tất cả các khu vực (section) của một sân cụ thể.
     * 
     * @param stadiumId ID của sân
     * @return danh sách các khu vực của sân
     */
    public List<Section> getSectionsByStadium(String stadiumId) {
        return sectionRepo.findByStadiumId(stadiumId);
    }

    /**
     * Tính tổng sức chứa của một sân (tổng số ghế).
     * 
     * @param stadiumId ID của sân
     * @return tổng sức chứa (số ghế)
     */
    public int getStadiumCapacity(String stadiumId) {
        List<Section> sections = getSectionsByStadium(stadiumId);
        return sections.stream()
                .mapToInt(section -> section.getTotalRows() * section.getSeatsPerRow())
                .sum();
    }

    /**
     * Tìm khu vực (section) theo ID.
     * 
     * @param sectionId ID của khu vực
     * @return Optional chứa khu vực nếu tìm thấy, ngược lại Optional rỗng
     */
    public Optional<Section> getSectionById(String sectionId) {
        return sectionRepo.findById(sectionId);
    }
}
