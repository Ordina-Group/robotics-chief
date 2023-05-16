<script lang="ts">
  import { currentAlert } from "$lib/alert";

  import "../app.postcss";
  import Alert from "../modules/Alert/Alert.svelte";
  import ModalManager from "../modules/ModalManager/ModalManager.svelte";
</script>

<div class="grid container mx-auto gap-1">
    <slot />

    <ModalManager />
    {#if $currentAlert !== undefined}
        <Alert color={$currentAlert.color} message={$currentAlert.message}/>
    {/if}
</div>

<style>
    @import url("https://fonts.googleapis.com/css2?family=Bruno+Ace+SC&display=swap");

    :global(*) {
        font-family: monospace;
    }

    :global(h1) {
        font-family: "Bruno Ace SC", Helvetica, sans-serif;
    }
</style>
