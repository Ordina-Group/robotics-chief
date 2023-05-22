<script lang="ts">
    import { settingsStore } from "$lib/dashboard";
</script>

<img
    id="livestream"
    src="http://{$settingsStore.host}:8080/livestream"
    alt="video"
    height="720"
    width="480"
/>
