<script lang="ts">
  import { Alert, Button, Input, Label } from "flowbite-svelte";

  import { closeModal } from "../ModalManager/modals";
  import { register, sendCommand } from "$lib/robot";

  const success = register("Message.CommandSuccess");
  const failure = register("Message.CommandFailure");

  export let ssid = "OrdinaNLGuest";
  let password = "";
  let connecting = false;
  let error: string | undefined = undefined;

  const connect = () => {
    connecting = true;
    $sendCommand({ type: "Command.ConnectWifi", ssid, password });
  };

  $: {
    if ($success?.command === "Command.ConnectWifi") {
      closeModal("connect_wifi");
      success.set(undefined);
      $sendCommand({ type: "Command.GetWifiNetworks" });
    }

    if ($failure?.command === "Command.ConnectWifi") {
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
