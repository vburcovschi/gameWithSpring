package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.awt.SystemColor.info;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Transactional
public class PlayerService {
    private PlayerRepository playerRepository;


    public PlayerService(@Autowired PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<Player> getPlayers(String name,
                                   String title,
                                   Race race,
                                   Profession profession,
                                   Integer minExperience,
                                   Integer maxExperience,
                                   Integer minLevel,
                                   Integer maxLevel,
                                   Long after,
                                   Long before,
                                   Boolean banned) {
        final Date afterDate = after==null ? new Date(1) :new Date(after);
        final Date beforeDate = before == null ? new Date() :new Date(before);
        final List<Player> list = new ArrayList<>();

        for(Player player: playerRepository.findAll()){

            if(!(name==null)&&!(player.getName().toLowerCase().contains(name.toLowerCase()))) continue;
            if(!(title==null)&&!(player.getTitle().toLowerCase().contains(title.toLowerCase())))continue;
            if(!(before==null)&&player.getBirthday().after(beforeDate)) continue;
            if(!(after==null)&&player.getBirthday().before(afterDate)) continue;
            if(!(minExperience==null)&&player.getExperience()<minExperience) continue;
            if(!(maxExperience==null)&&player.getExperience()>maxExperience) continue;
            if(!(minLevel==null)&&player.getLevel()<minLevel)continue;
            if(!(maxLevel==null)&&player.getLevel()>maxLevel)continue;
            if(!(race==null)&&!player.getRace().equals(race))continue;
            if(!(profession==null)&&!player.getProfession().equals(profession))continue;
            if((banned!=null)&&!player.getBanned().equals(banned))continue;
            list.add(player);

        }
        return list;
    }

    public Boolean isPlayerValid(Player player){
        if (StringUtils.isEmpty(player.getName()) || player.getName().length() > 12) return false;
        if (player.getTitle().length() > 30)  return false;
        if (isNull(player.getRace())) return false;
        if (isNull(player.getProfession())) return false;
        Long currentDate = new Date().getTime();
        if (isNull(player.getBirthday()) || player.getBirthday().getTime() < 0 || player.getBirthday().getTime()>=currentDate) return false;
        int year = player.getBirthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();
        if (year < 2000 || year > 3000) return false;

        if (isNull(player.getExperience()) || player.getExperience()<0 || player.getExperience()>10000000) return false;
        return true;
    }

    public Integer calcLevel(Integer experience){
        if (isNull(experience) || (experience<0)){
            experience=0;
        }
        return (int)(Math.sqrt(2500+200*experience)-50)/100;
    }

    public Integer calcNextLevel(Integer level, Integer experience){
        if (isNull(experience) || (experience<0)){
            experience=0;
        }
        if (isNull(level) || (level<0)){
            level=0;
        }
        return 50*(level+1)*(level+2)-experience;
    }

    public Player savePlayer(Player player){
        return playerRepository.save(player);
    }

    public Player getPlayerById(Long id){
        return playerRepository.findById(id).orElse(null);
    }

    public List<Player> sortPlayers(List<Player> players, PlayerOrder playerOrder) {
        if (playerOrder!= null) {
            players.sort((player1, player2) -> {
                switch (playerOrder) {
                    case ID: return player1.getId().compareTo(player2.getId());
                    case NAME: return player1.getName().compareTo(player2.getName());
                    case EXPERIENCE: return player1.getExperience().compareTo(player2.getExperience());
                    case BIRTHDAY: return player1.getBirthday().compareTo(player2.getBirthday());
                    default: return 0;
                }
            });
        }
        return players;
    }

    public List<Player> getPage(List<Player> sortedList, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > sortedList.size()) to = sortedList.size();
        return sortedList.subList(from, to);
    }

    public Boolean checkEmptyInfo(Player player){
        //if (isNull(info)) return true;
        return isNull(player.getId()) &&
                isNull(player.getName()) &&
                isNull(player.getTitle()) &&
                isNull(player.getRace()) &&
                isNull(player.getProfession()) &&
                isNull(player.getBirthday()) &&
                isNull(player.getBanned()) &&
                isNull(player.getExperience()) &&
                isNull(player.getLevel()) &&
                isNull(player.getUntilNextLevel());
    }

    public Player mergePlayerToUpdateWithPlayer(Player playerToUpdate, Player player){
        if(isNull(playerToUpdate.getName()))  playerToUpdate.setName(player.getName());
        if(isNull(playerToUpdate.getTitle()))  playerToUpdate.setTitle(player.getTitle());
        if(isNull(playerToUpdate.getRace()))  playerToUpdate.setRace(player.getRace());
        if(isNull(playerToUpdate.getProfession())) playerToUpdate.setProfession(player.getProfession());
        if(isNull(playerToUpdate.getBirthday()))  playerToUpdate.setBirthday(player.getBirthday());
        if(isNull(playerToUpdate.getExperience()))  playerToUpdate.setExperience(player.getExperience());
        return playerToUpdate;
    }

    public Player updatePlayer(long id, Player newPlayer) {
        Player oldPlayer = playerRepository.findById(id).orElse(null);
        if (isNull(oldPlayer)) {
            return null;
        }

        boolean needUpdate = false;

        if (!StringUtils.isEmpty(newPlayer.getName()) && newPlayer.getName().length() <= 12) {
            oldPlayer.setName(newPlayer.getName());
            needUpdate = true;
        }
        if (!StringUtils.isEmpty(newPlayer.getTitle()) && newPlayer.getTitle().length() <= 30) {
            oldPlayer.setTitle(newPlayer.getTitle());
            needUpdate = true;
        }
        if (nonNull(newPlayer.getRace())) {
            oldPlayer.setRace(newPlayer.getRace());
            needUpdate = true;
        }
        if (nonNull(newPlayer.getProfession())) {
            oldPlayer.setProfession(newPlayer.getProfession());
            needUpdate = true;
        }

        if (nonNull(newPlayer.getExperience())) {
            oldPlayer.setExperience(newPlayer.getExperience());
            needUpdate = true;
        }
        if (nonNull(newPlayer.getBirthday())) {
            oldPlayer.setBirthday(newPlayer.getBirthday());
            needUpdate = true;
        }

        if (nonNull(newPlayer.getBanned())) {
            oldPlayer.setBanned(newPlayer.getBanned());
            needUpdate = true;
        }

        if (needUpdate) {
            Integer level = calcLevel(newPlayer.getExperience());
            oldPlayer.setLevel(level);
            oldPlayer.setUntilNextLevel(calcNextLevel(level, newPlayer.getExperience()));
            playerRepository.save(oldPlayer);
        }

        return oldPlayer;
    }

    public void deletePlayer(Player player) {
        playerRepository.delete(player);
    }
}
