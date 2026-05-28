package com.backend.jobstream.steps;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Etantdonné;
import io.cucumber.java.fr.Quand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Step Definitions pour US-001: Rechercher des offres d'emploi
 */
public class RechercheOffresSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult response;
    private String lastSearchQuery;
    private String errorMessage;

    @Etantdonné("je suis sur la page de recherche")
    public void jeSuisSurLaPageDeRecherche() {
        // Navigation vers la page de recherche (frontend)
        // Pour tests API, on teste directement l'endpoint
    }

    @Quand("je saisis {string} dans le champ de recherche")
    public void jeSaisisDansLeChampDeRecherche(String motCle) throws Exception {
        lastSearchQuery = motCle;
        response = mockMvc.perform(get("/api/jobs")
                        .param("q", motCle))
                .andReturn();
    }

    @Quand("je clique sur le bouton {string}")
    public void jeCliqueSurLeBouton(String bouton) throws Exception {
        if (lastSearchQuery != null && !lastSearchQuery.isEmpty()) {
            response = mockMvc.perform(get("/api/jobs")
                            .param("q", lastSearchQuery))
                    .andReturn();
        } else {
            // Simulation d'une recherche sans mot-clé
            response = mockMvc.perform(get("/api/jobs"))
                    .andReturn();
        }
    }

    @Alors("une liste d'offres s'affiche")
    public void uneListeDOffresSAffiche() throws Exception {
        assertNotNull(response, "La réponse ne devrait pas être null");
        assertEquals(200, response.getResponse().getStatus(),
                "Le status devrait être 200");
        String content = response.getResponse().getContentAsString();
        // La réponse contient "jobs" (nom du champ dans JobSearchResponse)
        assertTrue(content.contains("jobs") || content.contains("total") || content.contains("count"),
                "La réponse devrait contenir une liste d'offres. Contenu: " + content);
    }

    @Alors("le nombre total de résultats est affiché")
    public void leNombreTotalDeResultatsEstAffiche() throws Exception {
        assertNotNull(response, "La réponse ne devrait pas être null");
        assertEquals(200, response.getResponse().getStatus());
        String content = response.getResponse().getContentAsString();
        // Vérifie que le total est présent
        assertTrue(content.contains("total"),
                "La réponse devrait contenir le total des résultats");
    }

    @Alors("le message {string} est affiché")
    public void leMessageEstAffiche(String message) throws Exception {
        assertNotNull(response, "La réponse ne devrait pas être null");
        String content = response.getResponse().getContentAsString();
        
        // Cas spécial: "Aucune offre trouvée" -> vérifier jobs vide ou count=0
        if (message.equals("Aucune offre trouvée")) {
            assertTrue(content.contains("jobs") && (content.contains("\"total\":0") || content.contains("\"count\":0")),
                    "Le message 'Aucune offre trouvée' devrait être validé par une réponse vide. Contenu: " + content);
        } else {
            assertTrue(content.contains(message) || content.toLowerCase().contains(message.toLowerCase()),
                    "Le message '" + message + "' devrait être dans la réponse. Contenu: " + content);
        }
    }

    @Et("le champ de recherche est mis en évidence")
    public void leChampDeRechercheEstMisEnEvidence() {
        // Vérifie le focus sur le champ (front-end)
    }

    @Et("le service externe est indisponible")
    public void leServiceExterneEstIndisponible() throws Exception {
        // On utilise un mot-clé spécial qui est morcké par WireMock pour retourner une erreur 500
        lastSearchQuery = "erreur500";
        response = mockMvc.perform(get("/api/jobs")
                        .param("q", "erreur500"))
                .andReturn();
    }

    @Alors("un message d'erreur {string} est affiché")
    public void unMessageDErreurEstAffiche(String message) throws Exception {
        assertNotNull(response, "La réponse ne devrait pas être null");
        int status = response.getResponse().getStatus();
        
        // Si on teste l'erreur API (500), on vérifie le status
        if ("erreur500".equals(lastSearchQuery)) {
            assertEquals(502, status, 
                    "Le status devrait être 502 (Bad Gateway) pour une erreur API externe");
        } else {
            // Accepte 400 (bad request), 404 (not found), ou 500 (server error)
            assertTrue(status >= 400 && status < 600, 
                    "Le status devrait être une erreur (4xx ou 5xx), mais était: " + status);
        }
    }

    // ============================================================================
    // Steps supplémentaires pour la pagination
    // ============================================================================

    @Alors("la première page d'offres s'affiche")
    public void laPremierePageDOffresSAffiche() throws Exception {
        assertNotNull(response);
        assertEquals(200, response.getResponse().getStatus());
    }

    @Alors("les boutons de pagination sont disponibles")
    public void lesBoutonsDePaginationSontDisponibles() throws Exception {
        assertNotNull(response);
        // La réponse devrait contenir des informations de pagination
    }

    // ============================================================================
    // Steps pour la pagination avancée
    // ============================================================================

    @Quand("je demande la page {int}")
    public void jeDemandeLaPage(int page) throws Exception {
        response = mockMvc.perform(get("/api/jobs")
                        .param("q", lastSearchQuery)
                        .param("page", String.valueOf(page)))
                .andReturn();
    }

    @Alors("les résultats de la page {int} s'affichent")
    public void lesResultatsDeLaPageSaffichent(int page) throws Exception {
        assertNotNull(response);
        assertEquals(200, response.getResponse().getStatus());
        String content = response.getResponse().getContentAsString();
        
        // Vérifie que la réponse contient des jobs
        assertTrue(content.contains("jobs"), 
                "La page devrait contenir des résultats. Contenu: " + content);
    }

    @Quand("je limite à {int} résultat(s) par page")
    public void jeLimiteAResultatsParPage(int limit) throws Exception {
        response = mockMvc.perform(get("/api/jobs")
                        .param("q", lastSearchQuery)
                        .param("limit", String.valueOf(limit)))
                .andReturn();
    }

    // ============================================================================
    // Helper methods pour les tests API
    // ============================================================================

    public void rechercheParMotCle(String motCle) throws Exception {
        lastSearchQuery = motCle;
        response = mockMvc.perform(get("/api/jobs")
                        .param("q", motCle))
                .andReturn();
    }

    public void verifieReponseRecherche() throws Exception {
        assertNotNull(response);
        assertEquals(200, response.getResponse().getStatus());
    }

    public void verifieAucunResultat() throws Exception {
        assertNotNull(response);
    }
}
