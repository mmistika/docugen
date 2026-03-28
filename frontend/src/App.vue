<script setup>
import { useAuth0 } from '@auth0/auth0-vue';
import { ref } from 'vue';

const { loginWithRedirect, logout, user, isAuthenticated, getAccessTokenSilently } = useAuth0();
const apiResponse = ref('');

const login = () => {
  loginWithRedirect();
};

const handleLogout = () => {
  logout({ logoutParams: { returnTo: window.location.origin } });
};

const callPrivateApi = async () => {
  try {
    const token = await getAccessTokenSilently();

    const response = await fetch('http://localhost:8080/api/user', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    if (response.ok) {
      apiResponse.value = await response.text();
    } else {
      apiResponse.value = `Error: ${response.status} - ${response.statusText}`;
    }
  } catch (e) {
    apiResponse.value = `Exception: ${e.message}`;
  }
};
</script>

<template>
  <div>
    <h1>Vue + Spring Boot + Auth0</h1>

    <div v-if="!isAuthenticated">
      <button @click="login">Log In</button>
    </div>

    <div v-if="isAuthenticated">
      <h2>Welcome, {{ user.name }}</h2>
      <button @click="handleLogout">Log Out</button>
      <br><br>

      <button @click="callPrivateApi">Call Protected Backend API</button>

      <div v-if="apiResponse">
        <h3>Backend Response:</h3>
        <p>{{ apiResponse }}</p>
      </div>
    </div>
  </div>
</template>