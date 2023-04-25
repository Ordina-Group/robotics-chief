<script lang="ts">
  import { slide } from 'svelte/transition';

  import { roboStore } from "../lib/stores"
</script>

<style>
    .wrapper {
        display: grid;
        grid-template-columns: 1fr 10fr;
        grid-gap: 0.5em;
    }

    .message-container {
        display: grid;
    }

    .message {
        height: 1em;
        grid-column: 1/2;
        grid-row: 1/2
    }
</style>

<h1>Welcome to RoboChief</h1>

<p class="wrapper">
    Status:
    <span class="message-container">
        {#key $roboStore}
            <span class="message" transition:slide>{$roboStore}</span>
        {/key}
    </span>
</p>
