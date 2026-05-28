package com.jobstream.ai_analyzer_service.service;

import com.jobstream.ai_analyzer_service.exception.AiAnalysisException;
import com.jobstream.ai_analyzer_service.exception.CvLoadException;
import com.jobstream.dto.JobAnalysisResult;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class AiAnalyzerService {
    private final ChatModel chatModel;
    private final BeanOutputConverter<JobAnalysisResult> converter;
    @Value("classpath:cv.md")
    private Resource cvResource;
    
    private String cachedCvContent;

    public AiAnalyzerService(ChatModel chatModel) {
        this.converter = new BeanOutputConverter<>(JobAnalysisResult.class);
        this.chatModel = chatModel;
    }

    @PostConstruct
    public void init() {
        this.cachedCvContent = getCvContent();
        log.info("CV chargé et mis en cache avec succès");
    }

    public JobAnalysisResult analyze(String jobDescription) {
        Prompt prompt = getPrompt(jobDescription);
        Generation generation = chatModel.call(prompt).getResult();
        if(generation == null){
            log.error("Generation null, erreur lors du traitement par l'agent");
            throw new AiAnalysisException("Erreur lors de l'analyse IA : génération nulle");
        }
        return converter.convert(generation.getOutput().getText());
    }

    private Prompt getPrompt(String jobDescription) {
        String format = converter.getFormat();
        String systemInstruction = String.format("""
        Tu es un expert en recrutement IT sur le marché suisse.
        Analyse l'offre d'emploi par rapport au CV fourni.
        %s
        """, format);

        SystemMessage systemMessage = new SystemMessage(systemInstruction);
        String userContent = String.format("Offre d'emploi : %s \n\n CV : %s", jobDescription, cachedCvContent);
        UserMessage userMessage = new UserMessage(userContent);

        return new Prompt(List.of(systemMessage, userMessage));
    }

    private String getCvContent() {
        try {
            return cvResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CvLoadException("Impossible de lire le fichier CV Markdown", e);
        }
    }

}
