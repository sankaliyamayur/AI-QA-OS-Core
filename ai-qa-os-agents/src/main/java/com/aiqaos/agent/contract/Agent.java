package com.aiqaos.agent.contract;

import com.aiqaos.agent.model.AgentRequestDTO;
import com.aiqaos.agent.model.AgentResultDTO;

public interface Agent {
    AgentResultDTO execute(AgentRequestDTO request);
    String getAgentName();
}