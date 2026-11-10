package mg.cnaps.modele.service;

import mg.cnaps.modele.dto.BesoinServiceDTO;
import mg.cnaps.modele.dto.RevuDTO;
import mg.cnaps.modele.dto.TypeActionDTO;
import mg.cnaps.modele.repository.RevuRepository;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class RevuService {

	private final RevuRepository revuRepository;

	public RevuService(RevuRepository revuRepository) {
		this.revuRepository = revuRepository;
	}

	public RevuDTO trouverParIdAccDetail(String idAccDetail) {
		if (idAccDetail == null || idAccDetail.trim().isEmpty()) {
			throw new IllegalArgumentException("L'identifiant du relevé est obligatoire");
		}
		return revuRepository.findByIdAccDetail(idAccDetail.trim());
	}

	public List<BesoinServiceDTO> listerBesoinsService() {
		return revuRepository.findAllBesoinsService();
	}

	public List<TypeActionDTO> listerTypesAction() {
		return revuRepository.findAllTypesAction();
	}

	public int ajouterRevuAcc(RevuDTO revu) {
		if (revu == null) {
			throw new IllegalArgumentException("Les données du relevé sont obligatoires");
		}

		return revuRepository.insertRevuAcc(
				revu.getAgentMatricule(),
				revu.getCodeFonction(),
				revu.getCodeService(),
				revu.getSection(),
				revu.getApplication(),
				revu.getCategorie(),
				revu.getTypeCategorie(),
				revu.getIdBesoinService(),
				revu.getMotifSuppression());
	}
}
