package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/rest")
public class PLayerController {
    private final PlayerService playerService;

    public PLayerController(@Autowired PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/players")
    public List<Player> getAll(@RequestParam(required = false) String name,
                               @RequestParam(required = false) String title,
                               @RequestParam(required = false) Race race,
                               @RequestParam(required = false) Profession profession,
                               @RequestParam(required = false) Long after,
                               @RequestParam(required = false) Long before,
                               @RequestParam(required = false) Integer minExperience,
                               @RequestParam(required = false) Integer maxExperience,
                               @RequestParam(required = false) Integer minLevel,
                               @RequestParam(required = false) Integer maxLevel,
                               @RequestParam(required = false) Boolean banned,
                               @RequestParam(required = false) Integer pageNumber,
                               @RequestParam(required = false) Integer pageSize,
                               @RequestParam(required = false) PlayerOrder order) {
        name = isNull(name) ? "": name;
        title = isNull(title) ? "": title;
        after = isNull(after) ? 0L: after;
        before = isNull(before) ? new Date().getTime(): before;
        minExperience = isNull(minExperience) ? 0: minExperience;
        maxExperience = isNull(maxExperience) ? Integer.MAX_VALUE: maxExperience;
        minLevel = isNull(minLevel) ? 0: minLevel;
        maxLevel = isNull(maxLevel) ? Integer.MAX_VALUE: maxLevel;
        pageNumber = isNull(pageNumber) ? 0 : pageNumber;
        pageSize = isNull(pageSize) ? 3 : pageSize;
        List<Player> players = playerService.getPlayers(name, title, race, profession,minExperience,maxExperience,minLevel,
                maxLevel,after,before,banned);
        List<Player> sortedList = playerService.sortPlayers(players,order);
        return playerService.getPage(sortedList, pageNumber, pageSize);
    }

    @GetMapping("/players/count")
    public Integer getCount(@RequestParam(value = "name", required = false)String name,
                            @RequestParam(value = "title",required = false) String title,
                            @RequestParam(value = "race", required = false) Race race,
                            @RequestParam(value = "profession", required = false) Profession profession,
                            @RequestParam(value = "after", required = false) Long after,
                            @RequestParam(value = "before", required = false) Long before,
                            @RequestParam(value = "minExperience", required = false) Integer minExperience,
                            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                            @RequestParam(value = "minLevel", required = false) Integer minLevel,
                            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                            @RequestParam(value = "pageSize", required = false) Integer pageSize,
                            @RequestParam(value ="banned", required = false) Boolean banned) {
        return playerService.getPlayers(name, title, race, profession, minExperience,
                maxExperience, minLevel, maxLevel, after, before, banned).size();
    }

    @PostMapping("/players")
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if (!playerService.isPlayerValid(player))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        else {
            player.setBanned(!isNull(player.getBanned()) && player.getBanned());
            player.setLevel(playerService.calcLevel(player.getExperience()));
            player.setUntilNextLevel(playerService.calcNextLevel(player.getLevel(), player.getExperience()));
            player = playerService.savePlayer(player);
            return ResponseEntity.status(HttpStatus.OK).body((player));
        }
    }

    @GetMapping("/players/{ID}")
    public ResponseEntity<Player> getPlayer(@PathVariable("ID") long id) {
        if (id <= 0 || isNull(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        Player player = playerService.getPlayerById(id);
        if (isNull(player)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(player);
        }
    }

    @PostMapping("/players/{ID}")
    public ResponseEntity<Player> updatePlayer(@PathVariable("ID") long id,
                                               @RequestBody Player newPlayer) {

        if (playerService.checkEmptyInfo(newPlayer))
            return ResponseEntity.status(HttpStatus.OK).body(playerService.getPlayerById(id));
        if (id <= 0 || isNull(id)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        Player oldPlayer = playerService.getPlayerById(id);
        if (isNull(oldPlayer)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        newPlayer = playerService.mergePlayerToUpdateWithPlayer(newPlayer, oldPlayer);

        if (!playerService.isPlayerValid(newPlayer)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        else newPlayer = playerService.updatePlayer(id, newPlayer);

        if (isNull(newPlayer)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        else
            return ResponseEntity.status(HttpStatus.OK).body(newPlayer);
    }

    @DeleteMapping("/players/{ID}")
    public ResponseEntity delete(@PathVariable("ID") long id) {
        if (id <= 0 || isNull(id)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        Player player = playerService.getPlayerById(id);
        if (isNull(player)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else
        {
            playerService.deletePlayer(player);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }

}

