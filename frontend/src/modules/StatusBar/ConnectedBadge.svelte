<script lang="ts">
  import { Badge, Tooltip } from "flowbite-svelte";
  import { connected } from "$lib/socket";
</script>

<Badge large class="whitespace-nowrap" id="chief-connection">
    {#if $connected}
        🟢
    {:else}
        🔴
    {/if}
    Chief
</Badge>

<Tooltip placement="bottom" triggeredBy="#chief-connection">
    Connection to the chief server
</Tooltip>
