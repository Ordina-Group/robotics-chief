<script lang="ts">
  import { currentModal } from "$lib/modal";
  import { currentAlert } from "$lib/alert";

  import "../app.postcss";
  import Modal from "../modules/modal/Modal.svelte";
  import Alert from "../modules/Alert/Alert.svelte";
</script>

<div class="grid container mx-auto gap-1">
    <slot />

    {#if $currentModal !== undefined}
        <Modal resource={$currentModal} onClose={() => currentModal.set(undefined)} />
    {/if}
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
