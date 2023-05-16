<script lang="ts">
  import Modal from "../modal/Modal.svelte";

  import { closeModal, modals, values } from "./modals";
</script>

{#each $modals as modal, i}
    <Modal
        resource={modal}
        value={$values[i]}
        onClose={() => closeModal(modal)}
    />
{/each}
