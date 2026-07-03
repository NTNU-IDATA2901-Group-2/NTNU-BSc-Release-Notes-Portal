import { UserManager, WebStorageStateStore, type User } from "oidc-client-ts";
import { config } from "./constants";
import { computed, ref } from "vue";
import { jwtDecode } from "jwt-decode"

type DecodedAccessToken = Record<string, unknown>;

type UserProfileInfo = {
  given_name?: string;
  family_name?: string;
  name?: string;
};

const userStoreKey = `oidc.user:${config.OIDC_AUTHORITY}:${config.OIDC_CLIENT_ID}`;

const userManager = new UserManager({
  authority: config.OIDC_AUTHORITY,
  client_id: config.OIDC_CLIENT_ID,
  scope: config.OIDC_SCOPES,
  redirect_uri: `${globalThis.location.origin}/auth/callback`,
  post_logout_redirect_uri: `${globalThis.location.origin}/sign-in`,
  response_type: "code",
  userStore: new WebStorageStateStore({ store: globalThis.localStorage }),
  automaticSilentRenew: true,
  monitorSession: false,
});

export const isAuthenticated = ref<boolean>(false);
export const jwtToken = ref<string | undefined>(undefined);
export const jwtTokenDecoded = ref<DecodedAccessToken | undefined>(undefined);
export const userProfile = ref<UserProfileInfo | undefined>(undefined);
export const isAdmin = computed(() => {
  const roles = jwtTokenDecoded.value?.[config.OIDC_ROLES_CLAIM];
  return Array.isArray(roles) && roles.includes("Admin");
});

const applyUser = (user: User) => {
  isAuthenticated.value = true;
  jwtToken.value = user.access_token;
  jwtTokenDecoded.value = jwtDecode<DecodedAccessToken>(user.access_token);
  userProfile.value = {
    given_name: user.profile.given_name,
    family_name: user.profile.family_name,
    name: user.profile.name,
  };
};

const clearUser = () => {
  isAuthenticated.value = false;
  jwtToken.value = undefined;
  jwtTokenDecoded.value = undefined;
  userProfile.value = undefined;
};

userManager.events.addUserLoaded(applyUser);
userManager.events.addUserUnloaded(clearUser);
userManager.events.addSilentRenewError(() => {
  console.error('Failed to refresh token');
  clearUser();
});
userManager.events.addAccessTokenExpired(() => {
  console.warn('Token not refreshed, user is no longer authenticated');
  clearUser();
});

// Keep authentication state in sync when another tab signs in, out or renews
globalThis.addEventListener("storage", (event) => {
  if (event.key !== userStoreKey) return;
  userManager.getUser().then((user) => {
    if (user && !user.expired) {
      applyUser(user);
    } else {
      clearUser();
    }
  });
});

export const initAuth = async (): Promise<void> => {
  const user = await userManager.getUser();
  if (user && !user.expired) {
    applyUser(user);
  } else if (user?.refresh_token) {
    try {
      const renewed = await userManager.signinSilent();
      if (renewed) applyUser(renewed);
    } catch {
      await userManager.removeUser();
    }
  }
};

export const handleSigninCallback = async (): Promise<void> => {
  const user = await userManager.signinRedirectCallback();
  applyUser(user);
};

export const login = (): Promise<void> => {
  return userManager.signinRedirect();
};

export const logout = async (): Promise<void> => {
  const user = await userManager.getUser();
  if (user) {
    await userManager.signoutRedirect();
  } else {
    clearUser();
    globalThis.location.assign('/sign-in');
  }
};

let renewInFlight: Promise<User | null> | null = null;

export const getAccessToken = async (): Promise<string | undefined> => {
  let user = await userManager.getUser();
  if (user && (user.expired || (user.expires_in ?? 0) < 30)) {
    renewInFlight ??= userManager.signinSilent().finally(() => {
      renewInFlight = null;
    });
    try {
      user = await renewInFlight;
    } catch {
      await userManager.removeUser();
      return undefined;
    }
  }
  return user?.access_token;
};
