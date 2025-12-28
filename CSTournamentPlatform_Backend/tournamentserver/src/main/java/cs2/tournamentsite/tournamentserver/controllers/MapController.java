package cs2.tournamentsite.tournamentserver.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cs2.tournamentsite.tournamentserver.models.Map;
import cs2.tournamentsite.tournamentserver.services.MapService;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/maps")
@RequiredArgsConstructor
public class MapController {
    private final MapService mapService;

    @GetMapping("/{id}")
    public Map findMapById(@PathVariable Integer id) {
        return mapService.findMapById(id);
    }
    
    @GetMapping("/match/{id}")
    public List<Map> getMapsByMatchId(@PathVariable Integer id) {
        return mapService.findMapsByMatchId(id);
    }
    
}
