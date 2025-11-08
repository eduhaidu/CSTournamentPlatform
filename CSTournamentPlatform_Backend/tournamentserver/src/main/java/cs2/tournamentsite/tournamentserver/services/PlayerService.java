package cs2.tournamentsite.tournamentserver.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import cs2.tournamentsite.tournamentserver.models.Player;
import cs2.tournamentsite.tournamentserver.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {
    private final PlayerRepository playerRepository;

    public Player findPlayerById(Integer id) {
        Optional<Player> playerOpt = playerRepository.findById(id);
        if (playerOpt.isPresent()) {
            return playerOpt.get();
        } else {
            log.warn("Player with id {} not found", id);
            return null;
        }
    }

    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    public Player updatePlayer(Integer id, Player updatedPlayer) {
        Optional<Player> playerOpt = playerRepository.findById(id);
        if (playerOpt.isPresent()) {
            Player existingPlayer = playerOpt.get();
            existingPlayer.setNickname(updatedPlayer.getNickname());
            existingPlayer.setFirstName(updatedPlayer.getFirstName());
            existingPlayer.setLastName(updatedPlayer.getLastName());
            existingPlayer.setCountry(updatedPlayer.getCountry());
            existingPlayer.setDateOfBirth(updatedPlayer.getDateOfBirth());
            existingPlayer.setTeamId(updatedPlayer.getTeamId());
            existingPlayer.setJoinedOn(updatedPlayer.getJoinedOn());
            existingPlayer.setRole(updatedPlayer.getRole());
            return playerRepository.save(existingPlayer);
        } else {
            log.warn("Player with id {} not found for update", id);
            return null;
        }
    }

    public void deletePlayer(Integer id) {
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
        } else {
            log.warn("Player with id {} not found for deletion", id);
        }
    }


}
