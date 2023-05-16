<script lang="ts">
  import { Button, Input, Label } from "flowbite-svelte";

  import settings from "./Settings";
  import { closeModal } from "../ModalManager/modals";

  let key: String = "";
  let value: String = "";

  const handleSubmit = () => {
    settings.set(key, value);
    closeModal("set_setting");
  };
</script>

<form on:submit|preventDefault={handleSubmit}>
    <div>
        <Label for="key">Key</Label>
        <Input bind:value={key} id="key" type="text" />
    </div>
    <div>
        <Label for="value">Value</Label>
        <Input bind:value={value} id="value" type="text" />
    </div>
    <Button type="submit">Add</Button>
</form>
