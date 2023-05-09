<script lang="ts">
  import { Button, Input, Label } from "flowbite-svelte";

  import { currentModal } from "$lib/modal";

  import settings from "./Settings";

  let key: String = "";
  let value: String = "";

  const handleSubmit = () => {
    settings.set(key, value);
    currentModal.set(undefined);
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
