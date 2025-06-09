// src/services/api.js
import axios from 'axios';

export const apiBaseURL = 'http://localhost:8090/v1';

const api = axios.create({
  baseURL: `${apiBaseURL}`,
});

export const loginUser = async (email, password) => {
  try {
    const response = await api.post('/users/login', {
      "email": email,
      "password": password,
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const registerUser = async (email, login, password) => {
  try {
    const response = await api.post('/users/register', {
      "email": email,
      "login": login,
      "password": password,
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const deleteUser = async (jwtToken) => {
  try {
    const response = await api.delete('/users', {
      headers:{
        "Authorization": `Bearer ${jwtToken}`
    }});
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getProjectsByUserToken = async (jwtToken) => {
  try {
    const response = await api.get('/projects/user',{
      headers: {
        "Authorization": `Bearer ${jwtToken}`
      }
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const createProject = async (jwtToken, name, description, dueDate) => {
  try {
    const response = await api.post('/projects',
      {
        "name": name,
        "description": description,
        "due_date": dueDate
      },
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getTasksByProjectId = async (jwtToken, projectId) => {
  try {
    const response = await api.get(`/tasks/project/${projectId}`,
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const updateTask = async (jwtToken, taskId, { name=null, description=null, story_points=null, 
  status=null, next=null, isNextUpdated=false }) => {
  try {
    const requestBody = {
      ...(name != null && { name }),
      ...(description != null && { description }),
      ...(story_points != null && { story_points: story_points }),
      ...(status != null && { status }),
      ...(isNextUpdated && { next }),
      ...({ isNextUpdated }),
    };
    const response = await api.patch(`/tasks/${taskId}`, 
      requestBody,
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const createTask = async (jwtToken, projectId, name, description, storyPoints) => {
  try {
    const response = await api.post(`/tasks`, 
      {
        project_id: projectId,
        name: name,
        description: description,
        story_points: storyPoints
      },
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const deleteTask = async (jwtToken, taskId, projectId) => {
  try {
    const response = await api.delete(`/tasks`,
      {
        data: {
          project_id: projectId,
          task_id: taskId
        },
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const getTaskAssignees = async (jwtToken, projectId, taskId) => {
  try {
    const response = await api.post(`tasks/assignments/task`,
      {
        project_id: projectId,
        task_id: taskId
      },
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const assignUserToTask = async (jwtToken, userId, projectId, taskId) => {
  try {
    const response = await api.post(`tasks/assignments/new/${userId}`,
      {
        project_id: projectId,
        task_id: taskId
      },
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const removeAssignment = async (jwtToken, userId, projectId, taskId) => {
  try {
    const response = await api.delete(`tasks/assignments/${userId}`,
      {
        data: {
        project_id: projectId,
        task_id: taskId
        },
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const getProjectMembers = async (jwtToken, projectId) => {
  try {
    const response = await api.get(`projects/members/${projectId}`,
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const getTaskDependencies = async (jwtToken, taskId) => {
  try {
    const response = await api.get(`tasks/dependency/${taskId}`,
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const addTaskDependency = async (jwtToken, taskId, dependencyId) => {
  try {
    const response = await api.post(`tasks/dependency`,
      {
        dependency_id: dependencyId,
        task_id: taskId
      },
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const removeTaskDependency = async (jwtToken, taskId, dependencyId) => {
  try {
    const response = await api.delete(`tasks/dependency`,
      {
        data: {
        dependency_id: dependencyId,
        task_id: taskId
        },
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const getStatistics = async (jwtToken, projectId) => {
  try {
    const response = await api.get(`projects/statistics/${projectId}`,
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const getFullProjectInfo = async (jwtToken, projectId) => {
  try {
    const response = await api.get(`projects/info/${projectId}`,
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const updateProject = async (jwtToken, projectId, { name=null,
   description=null, dueDate=null, availableHours=null, 
   sumExperience=null, externalRiskProbability=null}) => {
  try {
    const requestBody = {
      ...(name != null && { name }),
      ...(description != null && { description }),
      ...(dueDate != null && { due_date: dueDate }),
      ...(availableHours != null && { available_hours: availableHours }),
      ...(sumExperience != null && { sum_experience: sumExperience }),
      ...(externalRiskProbability != null && { external_risk_probability: externalRiskProbability }),
    };
    const response = await api.patch(`projects/${projectId}`,
      requestBody,
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const changeProjectOwner = async (jwtToken, userEmail, projectId) => {
  try {
    const response = await api.patch(`projects/owner`,
      {
        user_email: userEmail,
        project_id: projectId
      },
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const removeProjectMember = async (jwtToken, userId, projectId) => {
  try {
    const response = await api.delete(`projects/members`,
      {
        data: {
          user_id: userId,
          project_id: projectId
        },
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}

export const addProjectMember = async (jwtToken, userEmail, projectId) => {
  try {
    const response = await api.post(`projects/members`,
      {
        user_email: userEmail,
        project_id: projectId
      },
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}


export const deleteProject = async (jwtToken, projectId) => {
  try {
    const response = await api.delete(`projects/${projectId}`,
      {
        headers: {
          "Authorization": `Bearer ${jwtToken}`
        }
      }
    );
    return response.data;
  } catch (error) {
    throw error;
  }
}
