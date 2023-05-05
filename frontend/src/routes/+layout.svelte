<script lang="ts">
  import { currentModal } from "$lib/modal";

  import "../app.postcss";
  import Modal from "../modules/modal/Modal.svelte";
</script>

<div class="grid container mx-auto gap-1">
    <slot />

    {#if $currentModal !== undefined}
        <Modal resource={$currentModal} onClose={() => currentModal.set(undefined)} />
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
