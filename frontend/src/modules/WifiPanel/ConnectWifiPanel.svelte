<script lang="ts">
  import { Alert, Button, Input, Label } from "flowbite-svelte";

  import { register, sendCommand } from "$lib/socket";
  import { closeModal } from "../ModalManager/modals";

  const success = register("Message.CommandSuccess");
  const failure = register("Message.CommandFailure");

  export let ssid = "OrdinaNLGuest";
  let password = "";
  let connecting = false;
  let error: string | undefined = undefined;

  const connect = () => {
    connecting = true;
    sendCommand({ type: "ConnectWifi", ssid, password });
  };

  $: {
    if ($success?.command === "ConnectWifi") {
      closeModal("connect_wifi");
      success.set(undefined);
      sendCommand({ type: "GetWifiNetworks" });
    }

    if ($failure?.command === "ConnectWifi") {
      connecting = false;
      error = $failure.message;
    }
  }
</script>

<form
    action="#"
    class="grid gap-2"
    on:submit|preventDefault={connect}
>
    <h1>Connect to WiFi</h1>
    <div>
        <Label for="ssid">SSID</Label>
        <Input bind:value={ssid} id="ssid" type="text" />
    </div>
    <div>
        <Label for="password">Password</Label>
        <Input bind:value={password} id="password" type="password" />
    </div>
    {#if error !== undefined}
        <Alert color="red">{error}</Alert>
    {/if}
    <Button disabled={connecting} type="submit">
        Connect
    </Button>
</form>
