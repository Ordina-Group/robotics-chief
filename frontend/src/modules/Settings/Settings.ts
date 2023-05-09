const settingsMap: Map<String, Setting<Type>> = new Map();
const wildSettingsMap: Map<(value: string) => Boolean, Setting<Type>> = new Map();

export enum Type {
  String,
  Number,
  Boolean,
}

export type TypeMap<T> = T extends Type.Boolean ? boolean
  : T extends Type.String ? string
    : T extends Type.Number ? number
      : never;

export interface Setting<T extends Type, V extends TypeMap<T> = TypeMap<T>> {
  key: string;
  type: T;
  default: V | undefined;
  description?: string;
}

export type AttributeKey<T> = string;

export const getAllSettingsAndValues = (): Map<Setting<Type>, any> => {
  const result = new Map();
  const settings = settingsMap.values();

  for (const setting of settings) {
    result.set(setting, get(setting.key));
  }

  const wildSettings = Array.from(wildSettingsMap.entries());
  for (let i = 0, len = localStorage.length; i < len; i++) {
    const key = localStorage.key(i);
    if (key !== null) {
      const setting = wildSettings.find(([matcher, setting]) => matcher(key))?.[1];
      if (setting) {
        result.set({
          ...setting,
          key,
        }, get(key));
      }
    }
  }

  return result;
};

const get = <T, V extends TypeMap<T> = TypeMap<T>>(key: AttributeKey<T>): V => {
  const setting = getSetting(key);
  const item = localStorage.getItem(key);

  if (item === null) {
    return setting.default as V;
  }

  return JSON.parse(item) as V;
};

const set = <T extends Type, V extends TypeMap<T> = TypeMap<T>>(key: AttributeKey<T>, value: infer V): void => {
  getSetting(key); // Ensure registered
  localStorage.setItem(key, JSON.stringify(value));
}

const getSetting = <T extends Type>(key: AttributeKey<T>): Setting<T> => {
  const setting = settingsMap.get(key) ?? [...wildSettingsMap.entries()].find(([matcher]) => matcher(key))?.[1];
  if (!setting) {
    throw Error("Setting not found, register it before use");
  }

  return setting as Setting<T>;
}

const toMatcher = (wildcard: String) => (value: string): Boolean => {
  let w = wildcard.replace(/[.+^${}()|[\]\\]/g, '\\$&'); // regexp escape
  const re = new RegExp(`^${w.replace(/\*/g,'.*').replace(/\?/g,'.')}$`,'i');

  return re.test(value);
};

const register = (setting: Setting<any>) => {
  if (setting.key.includes("*")) {
    wildSettingsMap.set(toMatcher(setting.key), setting);
  } else {
    settingsMap.set(setting.key, setting);
  }
}

register({
  key: "bluetooth.device.*.name",
  type: Type.String,
  default: undefined,
  description: "",
});

export default {
  get,
  set,
  getSetting,
  register,
};
